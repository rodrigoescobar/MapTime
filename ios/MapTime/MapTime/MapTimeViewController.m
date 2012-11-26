//
//  MapTimeViewController.m
//  MapTime
//
//  Created by Nicholas Angeli & Rodrigo Escobar on 30/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "MapTimeViewController.h"
#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>
#import "TBXML.h"
#import "LongLatPair.h"
#import "TimeLineDownloaderDelegate.h"
#import "MapPoint.h"
#import "MBProgressHUD.h"
#import "MainViewController.h"

#define degreesToRadians( degrees ) ( ( degrees ) / 180.0 * M_PI )
#define METERS_PER_MILE 1609.344
#define RADIUS_OF_EARTH 6371
#define PROXIMITY_TO_FIRE 1

@implementation MapTimeViewController

@synthesize fromLocation;
@synthesize toLocation;
@synthesize timeLine;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [self registerForNotifications];
    
    
    mapView = (MKMapView *)[self.view viewWithTag:1001];
    [mapView setCenterCoordinate: CLLocationCoordinate2DMake(51.944942, -0.428467)];
    mapView.showsUserLocation = YES;
    
    [self initLocationManager];
    
    distanceBetweenLongLatPairs = [[NSMutableArray alloc] initWithCapacity:30];
    cumulativeDistanceBetweenPairs = [[NSMutableArray alloc] initWithCapacity:30]; // holds the cumulative distance between long lat pairs
    
    longLatPairs = [[NSMutableArray alloc] initWithCapacity:30];
    coordinates = [[NSMutableArray alloc] initWithCapacity:4];
    geofenceRegions = [[NSMutableArray alloc] initWithCapacity:30];
    
    numberOfPoints = 0;
    
    UILongPressGestureRecognizer *longPressGestureRecognizer = [[UILongPressGestureRecognizer alloc]initWithTarget:self action:@selector(handleGesture:)];
    longPressGestureRecognizer.minimumPressDuration = 0.8;
    [mapView addGestureRecognizer:longPressGestureRecognizer];
    
    if(![fromLocation isEqualToString:@""] && ![toLocation isEqualToString:@""]) {
        [self forwardGeocode];
    }
    
}

-(void)registerForNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(useNotAbleToDrawRouteNotification) name:@"NotAbleToDrawRoute" object:nil];
}

-(void)initLocationManager
{
    // Are location services enabled?
    if(![CLLocationManager locationServicesEnabled]) {
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:self.view];
        hud.mode = MBProgressHUDModeText;
        hud.detailsLabelText = @"Location services are required for this app to function.";
        [mapView addSubview:hud];
        [hud showWhileExecuting:@selector(waitForFourSeconds) onTarget:self withObject:nil animated:YES];
        return;
    }
    
    locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    
    locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    locationManager.distanceFilter = 1;
    
    [locationManager startUpdatingLocation];
}

-(void)forwardGeocode
{
    CLGeocoder *geocoder = [[CLGeocoder alloc] init]; // creates a geocoder object
    
    __block NSMutableArray *points = [[NSMutableArray alloc] initWithCapacity:4]; // stores the longlat of start and end, needs the __block modifier as is used in below blocks
    
    [geocoder geocodeAddressString:fromLocation completionHandler:^(NSArray *placemarks, NSError *error) { // forward geocode the address from the from field
        CLLocation *location = [placemarks[0] location];
        [points addObject:[[NSNumber alloc] initWithFloat:location.coordinate.latitude]]; // add the longitude
        [points addObject:[[NSNumber alloc] initWithFloat:location.coordinate.longitude]]; // add the latitude
        
        [geocoder geocodeAddressString:toLocation completionHandler:^(NSArray *placemarks, NSError *error) { // once from location has finished, perform forward geocode on address in toField
            CLLocation *location = [placemarks[0] location];
            [points addObject:[[NSNumber alloc] initWithFloat:location.coordinate.latitude]];
            [points addObject:[[NSNumber alloc] initWithFloat:location.coordinate.longitude]];
            
            [self downloadNavigationData:points]; // once all geocoding has complete, download all the data and kick it all off
        }];
        
    }];
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}

-(void)downloadNavigationData:(NSMutableArray *)array
{
    mbhud = [[MBProgressHUD alloc] init];
    mbhud.labelText = @"Getting navigation data";
    [mapView addSubview:mbhud];
    [mbhud show:YES];
    xmlData = [[NSMutableData alloc] init];
    
    NSString *point1 =[array objectAtIndex:0];
    NSString *point2 =[array objectAtIndex:1];
    NSString *point3 =[array objectAtIndex:2];
    NSString *point4 =[array objectAtIndex:3];

    NSString *urlString = [[NSString alloc] initWithFormat:@"http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=%@&flon=%@&tlat=%@&tlon=%@&v=motorcar&fast=1&layer=mapnik" , point1, point2, point3, point4];
    NSURL *url = [NSURL URLWithString:urlString];
    
    NSURLRequest *request = [[NSURLRequest alloc] initWithURL:url cachePolicy:nil timeoutInterval:10];
    
    (void) [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
}

- (void)handleGesture:(UIGestureRecognizer *)gestureRecognizer
{
    
    if (gestureRecognizer.state != UIGestureRecognizerStateEnded)
        return;
    
    CGPoint touchPoint = [gestureRecognizer locationInView:mapView];
    CLLocationCoordinate2D touchMapCoordinate =
    [mapView convertPoint:touchPoint toCoordinateFromView:mapView];
    
    MKPointAnnotation *pointAnnotation = [[MKPointAnnotation alloc] init];
    pointAnnotation.coordinate = touchMapCoordinate;
    pointAnnotation.title = [[NSString alloc] initWithFormat:@"%f, %f", pointAnnotation.coordinate.latitude, pointAnnotation.coordinate.longitude];
    
    if (numberOfPoints == 2){
        [mapView removeAnnotations:mapView.annotations];
        [mapView removeOverlays: mapView.overlays];
        [longLatPairs removeAllObjects];
        [cumulativeDistanceBetweenPairs removeAllObjects];
        [distanceBetweenLongLatPairs removeAllObjects];
        [coordinates removeAllObjects];
        numberOfPoints = 0;
    }
    if (numberOfPoints == 0) {
        [mapView addAnnotation:pointAnnotation];
        [coordinates addObject:[NSNumber numberWithDouble:pointAnnotation.coordinate.latitude]];
        [coordinates addObject:[NSNumber numberWithDouble:pointAnnotation.coordinate.longitude]];
        numberOfPoints++;

    } else {
        [mapView addAnnotation:pointAnnotation];
        [coordinates addObject:[NSNumber numberWithDouble:pointAnnotation.coordinate.latitude]];
        [coordinates addObject:[NSNumber numberWithDouble:pointAnnotation.coordinate.longitude]];
        numberOfPoints++;
        [self downloadNavigationData:coordinates];
    }
}

		
-(NSString *)parseXML:(NSData *)xml
{
    // method that parses the XML nabigation data
    NSError *error;
    TBXML *tbxml = [TBXML newTBXMLWithXMLData:xml error:&error];
    if(error) {
        NSLog(@"I AM FAILLING HERE!");
        NSLog(@"%@ %@", [error localizedDescription], [error userInfo]);
    } else {
        TBXMLElement *Document = [TBXML childElementNamed:@"Document" parentElement:tbxml.rootXMLElement];
        TBXMLElement *distance = [TBXML childElementNamed:@"distance" parentElement:Document];
        distanceBetweenPoints = [TBXML textForElement:distance];
        TBXMLElement *Folder = [TBXML childElementNamed:@"Folder" parentElement:Document];
        TBXMLElement *Placemark = [TBXML childElementNamed:@"Placemark" parentElement:Folder];
        TBXMLElement *LineString = [TBXML childElementNamed:@"LineString" parentElement:Placemark];
        TBXMLElement *coords = [TBXML childElementNamed:@"coordinates" parentElement:LineString];
        if([[TBXML textForElement:coords] isEqualToString:@""]) {
            [self useNotAbleToDrawRouteNotification];
            return @"NoData";
        } else {
            return [TBXML textForElement:coords];
        }
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
    MKMapPoint *points = malloc(sizeof(CLLocationCoordinate2D)*count);
    for(int i = 0; i < count; i++)
    {
        LongLatPair *pair = [longLatPairs objectAtIndex:i];
        [self calculateDistanceInBetween:i:count];
        CLLocationCoordinate2D coor = CLLocationCoordinate2DMake([[pair getLatitude] doubleValue], [[pair getLongitude] doubleValue]);
        MKMapPoint point = MKMapPointForCoordinate(coor);
        points[i] = point;        
    }
    
    // The Route Has Been Plotted and the timepoints should be added here
    MKPolyline *line = [MKPolyline polylineWithPoints:points count:count];
    [mapView addOverlay:line];
    [self populateCumulativeTotal];
    [self dropTimePoints];
}

- (void)calculateDistanceInBetween:(int) index:(int)count
{
    if(index < count-1) {
        LongLatPair *current = [longLatPairs objectAtIndex:index];
        LongLatPair *next = [longLatPairs objectAtIndex:index+1];
        NSNumber *distanceBetween = [[NSNumber alloc] initWithFloat:[self distanceBetween:current and:next]];
        [distanceBetweenLongLatPairs addObject:distanceBetween];
    }
}

-(void)populateCumulativeTotal
{    
    int count = 0;
    for(int i = 0; i < distanceBetweenLongLatPairs.count; i++) {
        if(count == 0) {
            [cumulativeDistanceBetweenPairs addObject:[distanceBetweenLongLatPairs objectAtIndex:0]];
        } else {
            float cumulativeDistance = [[cumulativeDistanceBetweenPairs objectAtIndex:i-1] floatValue] + [[distanceBetweenLongLatPairs objectAtIndex:i] floatValue];
            [cumulativeDistanceBetweenPairs addObject:[[NSNumber alloc] initWithFloat:cumulativeDistance]];
        }
        count++;
    }
}

-(void)dropTimePoints
{
    
    /*
     
     We first need to work out the percentage along the path, that the point we will be dropping will be placed at. To do this, 
     
     yearInBc - startYear / (abs(startYear) + abs(endYear)
     
     */
    
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.mode = MBProgressHUDModeAnnularDeterminate;
    [self.view addSubview:hud];
    hud.labelText = @"Placing TimePoints";
    
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_LOW, 0), ^{
        // Do something...
        
        NSMutableArray *timePoints = [timeLine getTimePoints];
        
        NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
        [f setNumberStyle:NSNumberFormatterDecimalStyle];
        
        NSNumber *firstYear = [f numberFromString:[[timePoints objectAtIndex:0] getYearInBc]];
        NSNumber *lastYear = [f numberFromString:[[timePoints objectAtIndex:([timePoints count]-1)] getYearInBc]];
        
        float diff = fabsf([firstYear floatValue]) + fabsf([lastYear floatValue]);
        
        // We now need to work out the distance between each of our long and lat pairs, so we can work out where to place the timepoints
        
        int count = 0;
        for(TimePoint *tp in timePoints) {
           // NSLog(@"%i", count);
            NSNumber *bcYear = [f numberFromString:[tp getYearInBc]];
            float percentage = ([bcYear floatValue] - [firstYear floatValue]) / diff;
            float distance = [[f numberFromString:distanceBetweenPoints] floatValue];
            float distanceToDrawPoint = distance * percentage;

            [self plot:percentage distance:distanceToDrawPoint timepoint:tp];
            count++;
        }

        
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [MBProgressHUD hideHUDForView:self.view animated:YES];
            // Register the CLRegions with the CLocationManger here
            [self initRegionMonitoring];
        });
    });
    
}

// which index of the cumulativeBetweenPairs does our point fall between? Hideous I know. 
-(int)whichIndex:(float)distance
{
    for(int i = 0; i < cumulativeDistanceBetweenPairs.count; i++) {
        if([[cumulativeDistanceBetweenPairs objectAtIndex:i] floatValue] > distance) {
            return i-1;
        }
    }
    return cumulativeDistanceBetweenPairs.count;
}



-(void)plot:(float)percentage distance:(float)distance timepoint:(TimePoint *)tp
{
    NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
    [f setNumberStyle:NSNumberFormatterDecimalStyle];
    
    NSNumber *total = [cumulativeDistanceBetweenPairs objectAtIndex:cumulativeDistanceBetweenPairs.count-1];
    
    int index = [self whichIndex:distance];

   // NSLog(@"INDEX IS: %i", index);

    if(index == -1) {
        index = 1;
    }
    if(index == 0) {
        index = 1;
    }
    LongLatPair *start = [longLatPairs objectAtIndex:index-1];
    LongLatPair *end = [longLatPairs objectAtIndex:index];

    
    NSNumber *cumStart = [cumulativeDistanceBetweenPairs objectAtIndex:index-1];
    
    float cumStartPercentage = [cumStart floatValue] / [total floatValue];
    
    float deltaPercentage = percentage - cumStartPercentage;
    
    float deltaLong = [[start getLongitude] floatValue] - [[end getLongitude] floatValue];
    float deltaLat = [[start getLatitude] floatValue] - [[end getLatitude] floatValue];
    
    float longitude = [[start getLongitude] floatValue] + (deltaLong * deltaPercentage);
    float latitude = [[start getLatitude] floatValue] + (deltaLat * deltaPercentage);
    
    MKPointAnnotation *point = [[MKPointAnnotation alloc] init];
    point.coordinate = CLLocationCoordinate2DMake(latitude, longitude);
    point.title = NSLocalizedString([tp getName], "Title of the pin");
    CLRegion *region = [[CLRegion alloc] initCircularRegionWithCenter:point.coordinate radius:PROXIMITY_TO_FIRE identifier:[tp getName]];
    [geofenceRegions addObject:region];
    NSLog(@"New geofence region: Lat at: %f Long at: %f", latitude, longitude);
    
    [mapView addAnnotation:point];
                                  
}



-(float)distanceBetween:(LongLatPair *)pair1 and:(LongLatPair *)pair2
{
    // Implementation of the haversine formula for working out distance between long and lat points, taking into account the spherical nature of the earth.
    float dLat = degreesToRadians([[pair2 getLatitude] floatValue] - [[pair1 getLatitude] floatValue]);
    float dLon = degreesToRadians([[pair2 getLongitude] floatValue] - [[pair1 getLongitude] floatValue]);
    float lat1 = degreesToRadians([[pair1 getLatitude] floatValue]);
    float lat2 = degreesToRadians([[pair2 getLatitude] floatValue]);
    
    float a = sinf(dLat/2) * sinf(dLat/2) + sinf(dLon/2) * sinf(dLon/2) * cosf(lat1) * cosf(lat2);
    float c = 2 * atan2f(sqrtf(a), sqrtf(1-a));
    
    return RADIUS_OF_EARTH * c;
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
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:mapView];
    hud.mode = MBProgressHUDModeText;
    hud.labelText = @"An unknown error occured.";
    [mapView addSubview:hud];
    [hud showWhileExecuting:@selector(waitForFourSeconds) onTarget:self withObject:nil animated:YES];
}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    /*NSLog(@"Finished");
    [spinner stopAnimating];
    spinner.hidden = YES;
     */
    [mbhud removeFromSuperview];
    [mbhud show:NO];

    NSString *pairs = [[NSString alloc] initWithString:[self parseXML:xmlData]];
    if([pairs isEqualToString:@"NoData"]) {
    } else {
        [self plotRoute:pairs];
    }
}

-(void)initRegionMonitoring
{
    NSLog(@"I am in the initRegionMonitoring method");
    if(![CLLocationManager regionMonitoringAvailable]) {
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:self.view];
        hud.mode = MBProgressHUDModeText;
        hud.detailsLabelText = @"Region monitoring is not supported on your device.";
        [mapView addSubview:hud];
        [hud showWhileExecuting:@selector(waitForFourSeconds) onTarget:self withObject:nil animated:YES];
        return;
    }
    
    for(CLRegion *geofence in geofenceRegions)
    {
        NSLog(@"I found a new geofence %@", geofence.identifier);
        [locationManager startMonitoringForRegion:geofence];
    }
}

-(void)useNotAbleToDrawRouteNotification
{
    NSLog(@"I have recieved a NotAbleToDrawRoute Notification");
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:self.view];
    hud.mode = MBProgressHUDModeText;
    hud.detailsLabelText = @"Could not draw this route. Please try another.";
    [mapView addSubview:hud];
    NSLog(@"Do i make it here?");
    [hud showWhileExecuting:@selector(waitForFourSeconds) onTarget:self withObject:nil animated:YES];
}


-(void)waitForFourSeconds
{
    sleep(4);
    [self performSegueWithIdentifier:@"BackHome" sender:self];
}

-(void)mapView:(MKMapView *)aMapView didUpdateUserLocation:(MKUserLocation *)userLocation
{
    CLLocationCoordinate2D coor = CLLocationCoordinate2DMake(userLocation.coordinate.latitude, userLocation.coordinate.longitude);
    MKCoordinateRegion region = MKCoordinateRegionMakeWithDistance(coor , 800, 800);
    [aMapView setRegion:region animated:YES];
    NSLog(@"UPdated user location");
}

/**
 
 The following methods are the delegate methdods that handle entering and exiting regions
 
 */

-(void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region
{
    NSLog(@"Did enter region: %@", region.identifier);
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:self.view];
    hud.mode = MBProgressHUDModeText;
    hud.detailsLabelText = [[NSString alloc] initWithFormat:@"%@", region.identifier];
    [mapView addSubview:hud];
    [hud showWhileExecuting:@selector(waitTwo) onTarget:self withObject:nil animated:YES];
}

-(void)waitTwo
{
    sleep(2);
}

-(void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region
{
    NSLog(@"Did exit region: %@", region.identifier);
}

-(void)locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region
{
    NSLog(@"I did start monitoring for region: %@", region.identifier);
}

@end
