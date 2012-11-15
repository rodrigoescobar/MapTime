//
//  MainViewController.m
//  MapTime
//
//  Created by Nicholas Angeli on 07/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import "MainViewController.h"
#import "MBProgressHUD.h"
#import "TimeLineDownloaderDelegate.h"
#import <CoreLocation/CoreLocation.h>
#import <MapKit/MapKit.h>
#import "MapTimeViewController.h"

@implementation MainViewController

@synthesize toField;
@synthesize fromField;

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    picker = (UIPickerView *) [self.view viewWithTag:2000];
    timeLineData = [[NSMutableData alloc] init];
    timeLines = [[NSMutableArray alloc] initWithCapacity:30];
        
    NSData *data = [self downloadTimelines];
    [self parseXML:data];
    //self.navigationItem.hidesBackButton = YES;
}

-(NSData *)downloadTimelines
{
        
    NSURL *url = [[NSURL alloc] initWithString:@"http://kanga-na8g09c.ecs.soton.ac.uk/api/fetchAll.php"];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
        
    NSHTTPURLResponse *response = nil;
    NSError *error = nil;
        
    NSData *data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];

    return data;
}



-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)thePickerView
{
    return 1;
}

-(NSInteger)pickerView:(UIPickerView *)thePickerView numberOfRowsInComponent:(NSInteger)component
{
    return [timeLines count];
}

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return [[timeLines objectAtIndex:row] getName];
}

-(void)parseXML:(NSData *)xml
{
    TBXML *tbxml = [TBXML newTBXMLWithXMLData:xml error:nil];
    TBXMLElement * rootXMlElement = tbxml.rootXMLElement;
    
    if(rootXMlElement) {
        [self traverseElement:rootXMlElement->firstChild];
    }
    
}

-(void)traverseElement:(TBXMLElement *)element
{
    // should have the first child element of <timelines>, i.e. should be a timeline object
    do {
        
        if([[TBXML elementName:element] isEqualToString:@"timeline"]) { // sanity check, should always be
            NSString *name = [TBXML valueOfAttributeNamed:@"timelineName" forElement:element];
            TimeLine *tl = [[TimeLine alloc] initWithName:name];
           // NSLog(@"Timeline found with name: %@", [tl getName]);
            [self traverseTimeLineElement:element->firstChild withTimeLineObject:tl];
            [timeLines addObject:tl];
            
        }
        
    } while((element = element->nextSibling));
}

-(void)traverseTimeLineElement:(TBXMLElement *)element withTimeLineObject:(TimeLine *)timeLine
{
    // we should now just have one timeline object
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
            
            //NSLog(@"Adding TimePoint to timeline %@", timeLine);
            [timeLine addTimePoint:timePoint];
        }
        
    } while((element = element->nextSibling));
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return NO;
}


-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSLog(@"Prepare for segue is called");
    if([segue.identifier isEqualToString:@"MoveToMap"]) {
        MapTimeViewController *destinationViewController = segue.destinationViewController;
        destinationViewController.fromLocation = fromField.text;
        destinationViewController.toLocation = toField.text;
        
        // get the timeline that's been picked here from the UIPickerView *picker

        NSInteger row;
        row = [picker selectedRowInComponent:0];
        TimeLine *timeLine = [timeLines objectAtIndex:row];
        destinationViewController.timeLine = timeLine;
        
    }
}

@end
