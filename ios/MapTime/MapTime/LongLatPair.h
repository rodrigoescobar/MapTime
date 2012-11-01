//
//  LongLatPair.h
//  MapTime
//
//  Created by Nicholas Angeli on 01/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface LongLatPair : NSObject
{
    NSNumber *longitude, *latitude;
}

-(id)initWithLon:(NSNumber *)lon andWithLat:(NSNumber *)lat;

-(NSNumber *)getLongitude;

-(NSNumber *)getLatitude;

@end
