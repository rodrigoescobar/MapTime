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
    
    NSString *notificationName = @"TimeLinesDownloaded";
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(useNotification) name:notificationName object:nil];
    
    [self downloadTimelines];
    
}

-(void)downloadTimelines
{
    delegate = [[TimeLineDownloaderDelegate alloc] init];
    NSURL *url = [NSURL URLWithString:@"http://kanga-na8g09c.ecs.soton.ac.uk/api/fetchAll.php"];
    NSURLRequest *request = [[NSURLRequest alloc] initWithURL:url];
    (void) [[NSURLConnection alloc] initWithRequest:request delegate:delegate];
}


-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)thePickerView
{
    return 1;
}

-(NSInteger)pickerView:(UIPickerView *)thePickerView numberOfRowsInComponent:(NSInteger)component
{
   // return 1;
    return [[delegate getTimeLines] count];
}

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return [[[delegate getTimeLines] objectAtIndex:row] getName];
    //return @"HELLO?";
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
        TimeLine *timeLine = [[delegate getTimeLines] objectAtIndex:row];
        destinationViewController.timeLine = timeLine;
        
    }
}


-(void)useNotification
{
    NSLog(@"I have recieved a notifications");
    picker = (UIPickerView *) [self.view viewWithTag:2000];
    [picker reloadAllComponents];
    
    self.navigationItem.hidesBackButton = YES;
}

@end
