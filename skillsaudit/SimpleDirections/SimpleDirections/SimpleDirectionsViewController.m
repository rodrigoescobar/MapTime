//
//  SimpleDirectionsViewController.m
//  SimpleDirections
//
//  Created by Nicholas Angeli on 21/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>
#import "SimpleDirectionsViewController.h"

@interface SimpleDirectionsViewController ()

@end

@implementation SimpleDirectionsViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    Class itemClass = [MKMapItem class];
    if(itemClass && [itemClass respondsToSelector:@selector(openMapsWithItems:launchOptions:)]) {
        NSLog(@"Able");
        
        MKMapItem *currentLocation = [MKMapItem mapItemForCurrentLocation];
        
        MKPlacemark *place = [[MKPlacemark alloc] initWithCoordinate:CLLocationCoordinate2DMake(52.389011, -0.780029) addressDictionary:nil];
        
        MKMapItem *destinationItem = [[MKMapItem alloc] initWithPlacemark:place];
        destinationItem.name = @"Test";
        
        NSArray *mapItemsArray = [NSArray arrayWithObjects:currentLocation, destinationItem, nil];
        
        NSDictionary *dictionaryForDirections = [NSDictionary dictionaryWithObject:MKLaunchOptionsDirectionsModeDriving forKey:MKLaunchOptionsDirectionsModeKey];
        
        [MKMapItem openMapsWithItems:mapItemsArray launchOptions:dictionaryForDirections];
        
    }

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
