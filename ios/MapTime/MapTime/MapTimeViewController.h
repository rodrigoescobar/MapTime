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
#import <CoreLocation/CoreLocation.h>
#import "MBProgressHUD.h"

@interface MapTimeViewController : UIViewController <NSURLConnectionDelegate, CLLocationManagerDelegate>
{
    NSMutableArray *coordinates;
    MKMapView *mapView;
    NSMutableArray *longLatPairs;
    int numberOfPoints;
    NSMutableData *xmlData;
    UIActivityIndicatorView *spinner;
    NSString *distanceBetweenPoints; // KM distance between the plotted points
    NSMutableArray *distanceBetweenLongLatPairs;
    NSMutableArray *cumulativeDistanceBetweenPairs;
    MBProgressHUD *mbhud;
    CLLocationManager *locationManager;
    NSMutableArray *geofenceRegions;
}

@property (nonatomic, strong) NSString *fromLocation;
@property (nonatomic, strong) NSString *toLocation;
@property (nonatomic, strong) TimeLine *timeLine;

- (void)viewDidLoad;

-(void)forwardGeocode;

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer;

-(void)downloadNavigationData:(NSMutableArray *)array;

-(void)downloadTimeLineData;

- (void)handleGesture:(UIGestureRecognizer *)gestureRecognizer;

-(NSString *)parseXML:(NSData *)xml;

-(void)plotRoute:(NSString *)pairs;

-(void)drawRoute;

- (void)calculateDistanceInBetween:(int) index:(int)count;

-(void)populateCumulativeTotal;

-(void)dropTimePoints;

-(int)whichIndex:(float)distance;

-(void)plot:(float)percentage distance:(float)distance timepoint:(TimePoint *)tp;

-(float)distanceBetween:(LongLatPair *)pair1 and:(LongLatPair *)pair2;

- (void)didReceiveMemoryWarning;

-(MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id)overlay;

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response;
-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data;
-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error;
-(void)connectionDidFinishLoading:(NSURLConnection *)connection;

-(void)useNotAbleToDrawRouteNotification;

@end
