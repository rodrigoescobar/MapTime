//
//  MapTimeViewController.h
//  MapTimeTest
//
//  Created by Nicholas Angeli on 21/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MapTimeViewController : UIViewController <MKMapViewDelegate>

@property (nonatomic, strong) MKMapView *mapView;

@end
