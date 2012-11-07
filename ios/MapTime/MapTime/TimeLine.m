//
//  TimeLine.m
//  MapTime
//
//  Created by Nicholas Angeli on 02/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "TimeLine.h"

@implementation TimeLine

-(id)initWithName:(NSString *)aName
{
    self = [super init];
    if(self != nil) {
        name = aName;
        timePoints = [[NSMutableArray alloc] initWithCapacity:30];
    }
    return self;
}

-(void)addTimePoint:(TimePoint *)point
{
    [timePoints addObject:point];
}

-(NSString *)getName
{
    return name;
}

-(NSMutableArray *)getTimePoints
{
    return timePoints;
}


@end
