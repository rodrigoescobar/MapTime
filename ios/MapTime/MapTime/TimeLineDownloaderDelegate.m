//
//  TimeLineDownloaderDelegate.m
//  MapTime
//
//  Created by Nicholas Angeli on 02/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "TimeLineDownloaderDelegate.h"
#import "TBXML.h"


@implementation TimeLineDownloaderDelegate

-(id)init
{
    self = [super init];
    if(self != nil) {
        timeLineData = [[NSMutableData alloc] init];
    }
    return self;
}

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    NSLog(@"TIMELINE RESPONSE");
}

-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    NSLog(@"TIMELINE DATA RECEIVED");
    [timeLineData appendData:data];

}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    NSLog(@"TIMELINE ERROR");

}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    NSLog(@"Finished Downloading");

    [self parseXML:timeLineData];
}

-(void)parseXML:(NSData *)xml
{
    TBXML *tbxml = [TBXML newTBXMLWithXMLData:xml error:nil];
    TBXMLElement * rootXMlElement = tbxml.rootXMLElement;
    timeLines = [[NSMutableArray alloc] init];
       
    if(rootXMlElement) {
        [self traverseElement:rootXMlElement];
        
        for(TimeLine *tl in timeLines) {
            NSLog(@"%@", [tl getName]);
        }
        
    }
}

-(void)traverseElement:(TBXMLElement *)element
{
    do {
        if([[TBXML elementName:element] isEqualToString:@"timeline"]) {
            NSString *name = [TBXML valueOfAttributeNamed:@"id" forElement:element];
            TimeLine *timeLine = [[TimeLine alloc] initWithName:name];
            NSLog(@"TimeLine Found: %@", name);
            [self traverseTimePoints:element withTimeLine:timeLine];
            [timeLines addObject:timeLine];
            
           // codycodecodecodecode lucy is awesome. so very awesome. i love her. yay.
        }
        
        if(element->firstChild) {
            [self traverseElement:element->firstChild];
        }
                
    } while ((element = element->nextSibling));
}

-(void)traverseTimePoints:(TBXMLElement *)element withTimeLine:(TimeLine *)timeLine
{
    do {
        
        if([[TBXML elementName:element] isEqualToString:@"timepoint"]) {
            TBXMLElement *name = [TBXML childElementNamed:@"name" parentElement:element];
            TBXMLElement *description = [TBXML childElementNamed:@"description" parentElement:element];
            TBXMLElement *sourceName = [TBXML childElementNamed:@"sourceName" parentElement:element];
            TBXMLElement *sourceURL = [TBXML childElementNamed:@"sourceURL" parentElement:element];
            TBXMLElement *year = [TBXML childElementNamed:@"year" parentElement:element];
            TBXMLElement *yearUnitID = [TBXML childElementNamed:@"yearUnitID" parentElement:element];
            TBXMLElement *month = [TBXML childElementNamed:@"month" parentElement:element];
            TBXMLElement *day = [TBXML childElementNamed:@"day" parentElement:element];
            TBXMLElement *yearInBC = [TBXML childElementNamed:@"yearInBC" parentElement:element];

            NSString *timePointname = [TBXML textForElement:name];
            NSString *timePointDescription = [TBXML textForElement:description];
            NSString *timePointSourceName = [TBXML textForElement:sourceName];
            NSString *timePointSourceURL = [TBXML textForElement:sourceURL];
            NSString *timePointYear = [TBXML textForElement:year];
            NSString *timePointYearUnitId = [TBXML textForElement:yearUnitID];
            NSString *timePointMonth = [TBXML textForElement:month];
            NSString *timePointDay = [TBXML textForElement:day];
            NSString *timePointYearInBC = [TBXML textForElement:yearInBC];
            
            TimePoint *timePoint = [[TimePoint alloc] init];
            [timePoint setName:timePointname];
            [timePoint setDescription:timePointDescription];
            [timePoint setSourceName:timePointSourceName];
            [timePoint setSourceURL:timePointSourceURL];
            [timePoint setYear:timePointYear];
            [timePoint setYearUnitID:timePointYearUnitId];
            [timePoint setMonth:timePointMonth];
            [timePoint setDay:timePointDay];
            [timePoint setYearInBC:timePointYearInBC];
            
            [timeLine addTimePoint:timePoint];
            
        }
        
        if(element->firstChild) {
            [self traverseTimePoints:element->firstChild withTimeLine:timeLine];
        }
        
        
    } while((element = element->nextSibling));
}

@end
