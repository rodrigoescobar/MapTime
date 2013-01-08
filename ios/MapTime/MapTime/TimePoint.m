//
//  TimePoint.m
//  MapTime
//
//  Created by Nicholas Angeli on 02/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "TimePoint.h"

@implementation TimePoint

-(void)setName:(NSString *)aName
{
    name = aName;
}

-(void)setDescription:(NSString *)aDescription
{
    description = aDescription;
}

-(void)setSourceName:(NSString *)aSourceName
{
    sourceName = aSourceName;
}

-(void)setSourceURL:(NSString *)aSourceURL
{
    soureURL = aSourceURL;
}

-(void)setYear:(NSString *)aYear
{
    year = aYear;
}

-(void)setYearUnitID:(NSString *)aYearUnitID
{
    yearUnitID = aYearUnitID;
}

-(void)setMonth:(NSString *)aMonth
{
    month = aMonth;
}

-(void)setDay:(NSString *)aDay
{
    day = aDay;
}

-(void)setYearInBC:(NSString *)aYearInBC
{
    yearInBC = aYearInBC;
}

-(NSString *)getYearInBc
{
    return yearInBC;
}

-(NSString *)getName
{
    return name;
}

-(NSString *)getDescription
{
    return description;
}

-(NSString *)getLink {
    return soureURL;
}


@end
