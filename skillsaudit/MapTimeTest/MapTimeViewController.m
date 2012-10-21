//
//  MapTimeViewController.m
//  MapTimeTest
//
//  Created by Nicholas Angeli on 21/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <CoreLocation/CoreLocation.h>
#import <MapKit/MapKit.h>
#import "MapTimeViewController.h"
#import "MapPoint.h"

@interface MapTimeViewController ()

@end

@implementation MapTimeViewController

MKMapView *mapView;
NSMutableArray *mapPoints;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    mapView = (MKMapView *)[self.view viewWithTag:1001];
    mapView.mapType = MKMapTypeHybrid;
    mapView.showsUserLocation = YES;
    mapPoints = [[NSMutableArray alloc] init];
    [self insertMapPoints];
}
     
-(void)insertMapPoints
{
    MapPoint *point;
    point = [[MapPoint alloc] init];
    point->longitude = [[NSNumber alloc] initWithFloat:-1.439209];
    point->latitude = [[NSNumber alloc] initWithFloat:50.882243];
    [mapPoints insertObject:point atIndex:mapPoints.count];
    point = nil;
    
    point = [[MapPoint alloc] init];
    point->longitude = [[NSNumber alloc] initWithFloat:-1.387483];
    point->latitude = [[NSNumber alloc] initWithFloat:50.932744];
    [mapPoints insertObject:point atIndex:mapPoints.count];

    
    for(MapPoint* point in mapPoints) {
        [mapView addAnnotation:point];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


-(MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id<MKAnnotation>)annotation
{
    if([annotation isKindOfClass:[MKUserLocation class]]) {
        return nil;
    }
    
    if([annotation isKindOfClass:[MapPoint class]]) {
        MKPinAnnotationView *customPinView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"MapPoint"];
        customPinView.pinColor = MKPinAnnotationColorRed;
        customPinView.animatesDrop = YES;
        customPinView.canShowCallout = NO;
        
        
        
        return customPinView;
    }
    
    return nil;
}


@end
