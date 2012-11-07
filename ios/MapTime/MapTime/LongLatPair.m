//
//  LongLatPair.m
//  MapTime
//
//  Created by Nicholas Angeli on 01/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "LongLatPair.h"

@implementation LongLatPair



-(id)initWithLon:(NSNumber *)lon andWithLat:(NSNumber *)lat
{
    self = [super init];
    if(self != nil) {
        longitude = lon;
        latitude = lat;
    }
    return self;
}

-(NSNumber *)getLongitude
{
    return longitude;
}

-(NSNumber *)getLatitude
{
    return latitude;
}


@end
