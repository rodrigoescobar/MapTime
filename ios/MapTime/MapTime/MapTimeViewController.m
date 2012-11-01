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

@implementation MapTimeViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    mapView = (MKMapView *)[self.view viewWithTag:1001];
    longLatPairs = [[NSMutableArray alloc] initWithCapacity:30];
    coordinates = [[NSMutableArray alloc]init];
    
    [coordinates addObject: [NSNumber numberWithDouble:0.5000]];
    [coordinates addObject: [NSNumber numberWithDouble:3.0050]];
    [coordinates addObject: [NSNumber numberWithDouble:4.0040]];
    [coordinates addObject: [NSNumber numberWithDouble:5.0040]];
    
    points = malloc(sizeof(CLLocationCoordinate2D)*2);
    numberOfPoints = 0;

    UILongPressGestureRecognizer *lpgr = [[UILongPressGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(handleGesture:)];
    lpgr.minimumPressDuration = 1.0;  //user must press for half second
    [mapView addGestureRecognizer:lpgr];
      
//    NSData *xml = [[NSData alloc] initWithData:[self downloadData:coordinates]];
//    NSString *pairs = [[NSString alloc] initWithString:[self parseXML:xml]];
//    [self plotRoute:pairs];
    
    //[self parseXML:xml];
                    
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}

-(void)downloadData:(NSMutableArray *)array
{
    xmlData = [[NSMutableData alloc] init];
    
    NSString *point1 =[[array objectAtIndex:0] stringValue];
    NSString *point2 =[[array objectAtIndex:1] stringValue];
    NSString *point3 =[[array objectAtIndex:2] stringValue];
    NSString *point4 =[[array objectAtIndex:3] stringValue];
    NSString *urlString = [[NSString alloc] initWithFormat:@"http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=%@&flon=%@&tlat=%@&tlon=%@&v=motorcar&fast=1&layer=mapnik" , point1, point2, point3, point4];
    NSURL *url = [NSURL URLWithString:urlString];
    
    NSURLRequest *request = [[NSURLRequest alloc] initWithURL:url];
    
    (void) [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
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
        // Kick of the download of the data here!

        [self downloadData:coordinates];

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
        TBXMLElement *coords = [TBXML childElementNamed:@"coordinates" parentElement:LineString];
        return [TBXML textForElement:coords];
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
            
            MKMapPoint *pts = malloc(sizeof(CLLocationCoordinate2D)*2);
            pts[0] = point1;
            pts[1] = point2;
            
            MKPolyline *line = [MKPolyline polylineWithPoints:pts count:2];
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

/*
 Delegate methods that handle the asynchronous network connectivity
 */

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    NSLog(@"Responce received");
}

-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    [xmlData appendData:data];
}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    NSLog(@"FAILED %@", [error localizedDescription]);
}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    NSLog(@"Finished");
    NSString *pairs = [[NSString alloc] initWithString:[self parseXML:xmlData]];
    [self plotRoute:pairs];
}


@end
