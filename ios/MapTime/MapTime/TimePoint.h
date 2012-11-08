//
//  TimePoint.h
//  MapTime
//
//  Created by Nicholas Angeli on 02/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TimePoint : NSObject
{
    NSString *name;
    NSString *description;
    NSString *sourceName;
    NSString *soureURL;
    NSString *year;
    NSString *yearUnitID;
    NSString *month;
    NSString *day;
    NSString *yearInBC;
}

-(void)setName:(NSString *)aName;

-(void)setDescription:(NSString *)aDescription;

-(void)setSourceName:(NSString *)aSourceName;

-(void)setSourceURL:(NSString *)aSourceURL;

-(void)setYear:(NSString *)aYear;

-(void)setYearUnitID:(NSString *)aYearUnitID;

-(void)setMonth:(NSString *)aMonth;

-(void)setDay:(NSString *)aDay;

-(void)setYearInBC:(NSString *)aYearInBC;

-(NSString *)getYearInBc;

-(NSString *)getName;


@end
