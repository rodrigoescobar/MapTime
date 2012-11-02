//
//  MapTimeViewController.h
//  MapTime
//
//  Created by Nicholas Angeli on 30/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>

@interface MapTimeViewController : UIViewController
{
    NSMutableArray *coordinates;
    MKMapView *mapView;
    NSMutableArray *longLatPairs;
    MKMapPoint *points;
    int numberOfPoints;
    NSMutableData *xmlData;
    UIActivityIndicatorView *spinner;
}

-(void)downloadData:(NSMutableArray *)array;

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


@end
