//
//  MapPoint.m
//  MapTimeTest
//
//  Created by Nicholas Angeli on 21/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "MapPoint.h"

@implementation MapPoint

-(CLLocationCoordinate2D)coordinate
{
    CLLocationCoordinate2D theCoordinate;
    theCoordinate.latitude = [latitude floatValue];
    theCoordinate.longitude = [longitude floatValue];
    return theCoordinate;
}

@end
