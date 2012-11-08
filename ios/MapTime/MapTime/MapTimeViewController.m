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
#import "TimeLineDownloaderDelegate.h"
#import "MapPoint.h"
#import "MBProgressHUD.h"

#define degreesToRadians( degrees ) ( ( degrees ) / 180.0 * M_PI )


@implementation MapTimeViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    // as soon as the view has loaded, we should download the timeline/timepoint data from the server
    [self downloadTimeLineData];
    
    mapView = (MKMapView *)[self.view viewWithTag:1001];
    
    distanceBetweenLongLatPairs = [[NSMutableArray alloc] initWithCapacity:30];
    cumulativeDistanceBetweenPairs = [[NSMutableArray alloc] initWithCapacity:30]; // holds the cumulative distance between long lat pairs
    
    spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    spinner.color = [UIColor blackColor];
    spinner.center = CGPointMake(160, 240);
    spinner.hidesWhenStopped = NO;
    spinner.hidden = YES;
        
    [mapView addSubview:spinner];
    
    
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
                    
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}

-(void)downloadNavigationData:(NSMutableArray *)array
{

    spinner.hidden = NO;
    [spinner startAnimating];
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

-(void)downloadTimeLineData
{
    delegate = [[TimeLineDownloaderDelegate alloc] init];
    NSURL *url = [NSURL URLWithString:@"http://kanga-na8g09c.ecs.soton.ac.uk/api/fetchAll.php"];
    NSURLRequest *request = [[NSURLRequest alloc] initWithURL:url];
    (void) [[NSURLConnection alloc] initWithRequest:request delegate:delegate];
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
        [longLatPairs removeAllObjects];
        [mapView removeOverlays: mapView.overlays];
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

        [self downloadNavigationData:coordinates];

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
        TBXMLElement *distance = [TBXML childElementNamed:@"distance" parentElement:Document];
        distanceBetweenPoints = [TBXML textForElement:distance];
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
            
            NSNumber *distanceBetween = [[NSNumber alloc] initWithFloat:[self distanceBetween:first and:second]];
            [distanceBetweenLongLatPairs addObject:distanceBetween];
                    
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
    
    // The Route Has Been Plotted and the timepoints should be added here
    
    
    [self populateCumulativeTotal];
    [self dropTimePoints];
}

-(void)populateCumulativeTotal
{    
    int count = 0;
    for(int i = 0; i < distanceBetweenLongLatPairs.count; i++) {
        if(count == 0) {
            [cumulativeDistanceBetweenPairs addObject:[distanceBetweenLongLatPairs objectAtIndex:0]];
        } else {
            float cum = [[cumulativeDistanceBetweenPairs objectAtIndex:i-1] floatValue] + [[distanceBetweenLongLatPairs objectAtIndex:i] floatValue];
            [cumulativeDistanceBetweenPairs addObject:[[NSNumber alloc] initWithFloat:cum]];
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
    hud.labelText = @"Placing TimePoints";
    
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_LOW, 0), ^{
        // Do something...
        
        NSLog(@"Dropping time points");
        NSMutableArray *timeLines = [delegate getTimeLines];
        TimeLine *timeLine = [timeLines objectAtIndex:0];
        NSMutableArray *timePoints = [timeLine getTimePoints];
        
        NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
        [f setNumberStyle:NSNumberFormatterDecimalStyle];
        
        NSNumber *firstYear = [f numberFromString:[[timePoints objectAtIndex:0] getYearInBc]];
        NSNumber *lastYear = [f numberFromString:[[timePoints objectAtIndex:([timePoints count]-1)] getYearInBc]];
        
        float diff = fabsf([firstYear floatValue]) + fabsf([lastYear floatValue]);
        
        // We now need to work out the distance between each of our long and lat pairs, so we can work out where to place the timepoints
        
        int count = 0;
        for(TimePoint *tp in timePoints) {
            NSLog(@"%i", count);
            NSNumber *bcYear = [f numberFromString:[tp getYearInBc]];
            NSString *name = [tp getName];
            float percentage = ([bcYear floatValue] - [firstYear floatValue]) / diff;
            float distance = [[f numberFromString:distanceBetweenPoints] floatValue];
            float distanceToDrawPoint = distance * percentage;
            NSLog(@"%@ should be drawn %f%% from the start, which is: %f km", name, percentage, distanceToDrawPoint);
            // [self betweenWhichPointsIs:distanceToDrawPoint withPercentage:percentage];
            
            // [self plotPoint:percentage withDistanceToDraw:distanceToDrawPoint];
            [self plot:percentage distance:distanceToDrawPoint timepoint:tp];
            count++;
        }

        
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [MBProgressHUD hideHUDForView:self.view animated:YES];
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

    NSLog(@"INDEX IS: %i", index);

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
    point.title = NSLocalizedString(tp.getName, "Title of the pin");
    
    [mapView addAnnotation:point];
                                  
}



-(float)distanceBetween:(LongLatPair *)pair1 and:(LongLatPair *)pair2
{
    // Implementation of the haversine formula for working out distance between long and lat points, taking into account the spherical nature of the earth.
    int radius = 6371; // radius of earth in KM
    float dLat = degreesToRadians([[pair2 getLatitude] floatValue] - [[pair1 getLatitude] floatValue]);
    float dLon = degreesToRadians([[pair2 getLongitude] floatValue] - [[pair1 getLongitude] floatValue]);
    float lat1 = degreesToRadians([[pair1 getLatitude] floatValue]);
    float lat2 = degreesToRadians([[pair2 getLatitude] floatValue]);
    
    float a = sinf(dLat/2) * sinf(dLat/2) + sinf(dLon/2) * sinf(dLon/2) * cosf(lat1) * cosf(lat2);
    float c = 2 * atan2f(sqrtf(a), sqrtf(1-a));
    
    return radius * c;
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
    [spinner stopAnimating];
    spinner.hidden = YES;
    NSString *pairs = [[NSString alloc] initWithString:[self parseXML:xmlData]];
    [self plotRoute:pairs];
}


@end
