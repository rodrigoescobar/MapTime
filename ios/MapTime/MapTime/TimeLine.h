//
//  TimeLine.h
//  MapTime
//
//  Created by Nicholas Angeli on 02/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TimePoint.h"
#import "TimeLine.h"

@interface TimeLine : NSObject
{
    NSString *name;
    NSMutableArray *timePoints;
}

-(id)initWithName:(NSString *)aName;

-(void)addTimePoint:(TimePoint *)point;

-(NSString *)getName;

@end
