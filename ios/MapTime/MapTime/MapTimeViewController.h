//
//  MapTimeViewController.h
//  MapTime
//
//  Created by Nicholas Angeli on 30/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "TimeLineDownloaderDelegate.h"
#import "LongLatPair.h"

@interface MapTimeViewController : UIViewController
{
    NSMutableArray *coordinates;
    MKMapView *mapView;
    NSMutableArray *longLatPairs;
    int numberOfPoints;
    NSMutableData *xmlData;
    UIActivityIndicatorView *spinner;
    TimeLineDownloaderDelegate *delegate;
    NSString *distanceBetweenPoints; // KM distance between the plotted points
    NSMutableArray *distanceBetweenLongLatPairs;
    NSMutableArray *cumulativeDistanceBetweenPairs;
}

-(void)downloadNavigationData:(NSMutableArray *)array;

-(void)downloadTimeLineData;

-(NSString *)parseXML:(NSData *)xml;

-(void)plotRoute:(NSString *)pairs;

-(void)drawRoute;

- (void)handleGesture:(UIGestureRecognizer *)gestureRecognizer;

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer;

-(MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id)overlay;

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response;

-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data;

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error;

-(void)connectionDidFinishLoading:(NSURLConnection *)connection;

-(void)dropTimePoints;

-(float)distanceBetween:(LongLatPair *)pair1 and:(LongLatPair *)pair2;

-(NSArray *)betweenWhichPointsIs:(float)distanceToDraw withPercentage:(float)percentage;


@end
