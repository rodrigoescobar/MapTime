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

MKMapView *mapView;
NSMutableArray *longLatPairs;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    mapView = (MKMapView *)[self.view viewWithTag:1001];
    longLatPairs = [[NSMutableArray alloc] initWithCapacity:30];
    
    UILongPressGestureRecognizer *lpgr = [[UILongPressGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(handleGesture:)];
    lpgr.minimumPressDuration = 2.0;  //user must press for 2 seconds
    [mapView addGestureRecognizer:lpgr];
      
    NSData *xml = [[NSData alloc] initWithData:[self downloadData]];
    NSString *pairs = [[NSString alloc] initWithString:[self parseXML:xml]];
    [self plotRoute:pairs];
                    
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}

-(NSData *)downloadData
{
    NSURL *url = [NSURL URLWithString:@"http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=52.215676&flon=5.963946&tlat=52.2573&tlon=6.1799&v=motorcar&fast=1&layer=mapnik"];
    
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
    [mapView addAnnotation:pa];
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
