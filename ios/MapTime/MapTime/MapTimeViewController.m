//
//  MapTimeViewController.m
//  MapTime
//
//  Created by Nicholas Angeli on 30/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "MapTimeViewController.h"
#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>
#import "TBXML.h"
#import "LongLatPair.h"


@interface MapTimeViewController ()

@end

@implementation MapTimeViewController

NSMutableArray *coordinates; 
MKMapView *mapView;
NSMutableArray *longLatPairs;
MKMapPoint *points;
int numberOfPoints;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    mapView = (MKMapView *)[self.view viewWithTag:1001];
    longLatPairs = [[NSMutableArray alloc] initWithCapacity:30];
    coordinates = [[NSMutableArray alloc]init];
    points = malloc(sizeof(CLLocationCoordinate2D)*2);
    numberOfPoints = 0;

    UILongPressGestureRecognizer *lpgr = [[UILongPressGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(handleGesture:)];
    lpgr.minimumPressDuration = 1.0;  //user must press for half second
    [mapView addGestureRecognizer:lpgr];
      
    NSData *xml = [[NSData alloc] initWithData:[self downloadData]];
    NSString *pairs = [[NSString alloc] initWithString:[self parseXML:xml]];
    [self plotRoute:pairs];
    
    
    CLLocationDegrees lat1 = 50.876004;
    CLLocationDegrees lon1 = -1.373291;

    [coordinates addObject: [NSNumber numberWithDouble:lat1]];
    [coordinates addObject: [NSNumber numberWithDouble:lon1]];
    CLLocationCoordinate2D coords1 = CLLocationCoordinate2DMake(lat1, lon1);
    MKMapPoint point1 = MKMapPointForCoordinate(coords1);
    
    CLLocationDegrees lat2 = 51.475225;
    CLLocationDegrees lon2 = -0.131836;
    [coordinates addObject: [NSNumber numberWithDouble:lat2]];
    [coordinates addObject: [NSNumber numberWithDouble:lon2]];
    CLLocationCoordinate2D coords2 = CLLocationCoordinate2DMake(lat2, lon2);
    MKMapPoint point2 = MKMapPointForCoordinate(coords2);
    
    //MKMapPoint *points = malloc(sizeof(CLLocationCoordinate2D)*2);
    points[0] = point1;
    points[1] = point2;
    
    MKPolyline *line = [MKPolyline polylineWithPoints:points count:2];
    
    [mapView addOverlay:line];
    
    NSData *xml = [[NSData alloc] initWithData:[self downloadData:coordinates]];
    [self parseXML:xml];
                    
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}

-(NSData *)downloadData:(NSMutableArray *)array
{
    NSString *point1 =[[array objectAtIndex:0] stringValue];
    NSString *point2 =[[array objectAtIndex:1] stringValue];
    NSString *point3 =[[array objectAtIndex:2] stringValue];
    NSString *point4 =[[array objectAtIndex:3] stringValue];
    NSString *urlString = [[NSString alloc] initWithFormat:@"http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=%@&flon=%@&tlat=%@&tlon=%@&v=motorcar&fast=1&layer=mapnik" , point1, point2, point3, point4];
    NSURL *url = [NSURL URLWithString:urlString];
    
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    
    NSHTTPURLResponse *response = nil;
    NSError *error = nil;
    
    NSData *data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
    
    if(error != nil) {
        NSLog(@"%@", [error localizedDescription]);
    }
    
    return data;
    
}

- (void)handleGesture:(UIGestureRecognizer *)gestureRecognizer
{
    
    NSLog(@"I'm in the gesture recogniser");
    if (gestureRecognizer.state != UIGestureRecognizerStateEnded)
        return;
    
    CGPoint touchPoint = [gestureRecognizer locationInView:mapView];
    CLLocationCoordinate2D touchMapCoordinate =
    [mapView convertPoint:touchPoint toCoordinateFromView:mapView];
    
    MKPointAnnotation *pa = [[MKPointAnnotation alloc] init];
    pa.coordinate = touchMapCoordinate;
    pa.title = [[NSString alloc] initWithFormat:@"%f, %f", pa.coordinate.latitude, pa.coordinate.longitude];
    
    if (numberOfPoints == 2){
        [mapView removeAnnotations:mapView.annotations];
        numberOfPoints = 0;
    }
    if (numberOfPoints == 0) {
        [mapView addAnnotation:pa];
        [coordinates replaceObjectAtIndex: 0  withObject: [NSNumber numberWithDouble:pa.coordinate.latitude]];
        [coordinates replaceObjectAtIndex: 1  withObject: [NSNumber numberWithDouble:pa.coordinate.longitude]];
        CLLocationCoordinate2D coord = CLLocationCoordinate2DMake(pa.coordinate.latitude, pa.coordinate.longitude);
        MKMapPoint point = MKMapPointForCoordinate(coord);
        points[0] = point;
        numberOfPoints++;

    } else {
        [mapView addAnnotation:pa];
        CLLocationCoordinate2D coord = CLLocationCoordinate2DMake(pa.coordinate.latitude, pa.coordinate.longitude);
        [coordinates replaceObjectAtIndex: 2  withObject: [NSNumber numberWithDouble:pa.coordinate.latitude]];
         [coordinates replaceObjectAtIndex: 3  withObject: [NSNumber numberWithDouble:pa.coordinate.longitude]];
        MKMapPoint point = MKMapPointForCoordinate(coord);
        points[1] = point;
        numberOfPoints++;
    }
}



-(NSString *)parseXML:(NSData *)xml
{
    NSError *error;
    TBXML *tbxml = [TBXML newTBXMLWithXMLData:xml error:&error];
    if(error) {
        NSLog(@"%@ %@", [error localizedDescription], [error userInfo]);
    } else {
        TBXMLElement *Document = [TBXML childElementNamed:@"Document" parentElement:tbxml.rootXMLElement];
        TBXMLElement *Folder = [TBXML childElementNamed:@"Folder" parentElement:Document];
        TBXMLElement *Placemark = [TBXML childElementNamed:@"Placemark" parentElement:Folder];
        TBXMLElement *LineString = [TBXML childElementNamed:@"LineString" parentElement:Placemark];
        TBXMLElement *coordinates = [TBXML childElementNamed:@"coordinates" parentElement:LineString];
        return [TBXML textForElement:coordinates];
    }
    return nil;
}

-(void)plotRoute:(NSString *)pairs
{
    // pairs is the comma seperated value of long and lat pairs
    NSArray *lines = [pairs componentsSeparatedByString:@"\n"];
    for(NSString *str in lines)
    {
        NSArray *arr = [str componentsSeparatedByString:@","];
        NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
        [f setNumberStyle:NSNumberFormatterDecimalStyle];
        NSNumber *longitude = [f numberFromString:arr[0]];
        NSNumber *lattide = [f numberFromString:arr[1]];
        LongLatPair *pair = [[LongLatPair alloc] initWithLon:longitude andWithLat:lattide];

        [longLatPairs addObject:pair];
    }
    
    [self drawRoute];
}

-(void)drawRoute
{
    
    int count = longLatPairs.count;
    int index = 0; 
    for(int i = 0; i < count; i++)
    {
        
        index = i;
        index++;
        if(index < count) {

            LongLatPair *first = [longLatPairs objectAtIndex:i];
            LongLatPair *second = [longLatPairs objectAtIndex:index];
            CLLocationCoordinate2D co1 = CLLocationCoordinate2DMake([[first getLatitude] doubleValue], [[first getLongitude] doubleValue]);
            CLLocationCoordinate2D co2 = CLLocationCoordinate2DMake([[second getLatitude] doubleValue], [[second getLongitude] doubleValue]);
            
            MKMapPoint point1 = MKMapPointForCoordinate(co1);
            MKMapPoint point2 = MKMapPointForCoordinate(co2);
            
            MKMapPoint *points = malloc(sizeof(CLLocationCoordinate2D)*2);
            points[0] = point1;
            points[1] = point2;
            
            MKPolyline *line = [MKPolyline polylineWithPoints:points count:2];
            [mapView addOverlay:line];
        }
        
    }
     
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id)overlay
{
    if([overlay isKindOfClass:[MKPolyline class]]) {
        MKPolylineView *aView = [[MKPolylineView alloc] initWithPolyline:(MKPolyline *)overlay];
        aView.strokeColor = [[UIColor blueColor] colorWithAlphaComponent:0.7];
        return aView;
    }
    
    return nil;
}


@end
