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

@interface MapTimeViewController ()

@end

@implementation MapTimeViewController

MKMapView *mapView; 

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    
    mapView = (MKMapView *)[self.view viewWithTag:1001];
    
    UILongPressGestureRecognizer *lpgr = [[UILongPressGestureRecognizer alloc]
                                          initWithTarget:self action:@selector(handleGesture:)];
    lpgr.minimumPressDuration = 2.0;  //user must press for 2 seconds
    [mapView addGestureRecognizer:lpgr];
    
    CLLocationDegrees lat1 = 50.876004;
    CLLocationDegrees lon1 = -1.373291;
    CLLocationCoordinate2D coords1 = CLLocationCoordinate2DMake(lat1, lon1);
    MKMapPoint point1 = MKMapPointForCoordinate(coords1);
    
    CLLocationDegrees lat2 = 51.475225;
    CLLocationDegrees lon2 = -0.131836;
    CLLocationCoordinate2D coords2 = CLLocationCoordinate2DMake(lat2, lon2);
    MKMapPoint point2 = MKMapPointForCoordinate(coords2);
    
    MKMapPoint *points = malloc(sizeof(CLLocationCoordinate2D)*2);
    points[0] = point1;
    points[1] = point2;
    
    MKPolyline *line = [MKPolyline polylineWithPoints:points count:2];
    
    [mapView addOverlay:line];
    
    NSString *xml = [[NSString alloc] initWithString:[self downloadData]];
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}

-(NSString *)downloadData
{
    NSURL *url = [NSURL URLWithString:@"http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=52.215676&flon=5.963946&tlat=52.2573&tlon=6.1799&v=motorcar&fast=1&layer=mapnik"];
    
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    NSHTTPURLResponse *response = nil;
    NSError *error = nil;
    
    NSData *data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
    
    if(error != nil) {
        NSLog(@"%@", [error localizedDescription]);
    }
    
    NSString *string = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    return string;
    
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


-(MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id)overlay
{
    if([overlay isKindOfClass:[MKPolyline class]]) {
        MKPolylineView *aView = [[MKPolylineView alloc] initWithPolyline:(MKPolyline *)overlay];
        aView.strokeColor = [[UIColor blueColor] colorWithAlphaComponent:0.7];
        return aView;
    }
    
    return nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
