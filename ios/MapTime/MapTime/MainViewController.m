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
    self.navigationController.navigationBar.tintColor = [UIColor blackColor];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self registerForNotifications];
    [self downloadTimelines];
    [self fromFieldChanged];
}

-(void)registerForNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(useFinishedNotification) name:@"TimeLinesDownloaded" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(useErrorNotification) name:@"ErrorNotification" object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(toFieldChanged) name:UITextFieldTextDidChangeNotification object:toField];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fromFieldChanged) name:UITextFieldTextDidChangeNotification object:fromField];
    
}

-(void)toFieldChanged
{
    UIButton *button = (UIButton *)[self.view viewWithTag:2];
    if([toField.text compare:@"Current Location" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
        toField.textColor = [UIColor blueColor];
        button.hidden = YES;
    } else {
        toField.textColor = [UIColor blackColor];
        button.hidden = NO;
    }
}

-(void)fromFieldChanged
{
    UIButton *button = (UIButton *)[self.view viewWithTag:1];

    if([fromField.text compare:@"Current Location" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
        fromField.textColor = [UIColor blueColor];
        button.hidden = YES;
    } else {
        fromField.textColor = [UIColor blackColor];
        button.hidden = NO;
    }
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
    return [[delegate getTimeLines] count];
}

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return [[[delegate getTimeLines] objectAtIndex:row] getName];
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

-(void)waitForFourSeconds
{
    sleep(4);
}

/*
 Notification Methods
 */


-(void)useFinishedNotification
{
    NSLog(@"I have recieved a Finished notifications");
    picker = (UIPickerView *) [self.view viewWithTag:2000];
    [picker reloadAllComponents];
    
    self.navigationItem.hidesBackButton = YES;
}

-(void)useErrorNotification
{
    NSLog(@"I have recieved a Error Notification");
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:self.view];
    hud.mode = MBProgressHUDModeText;
    hud.detailsLabelText = @"Error occured connecting to TimeLines\n Please try again later.";
    [self.view addSubview:hud];
    [hud showWhileExecuting:@selector(waitForFourSeconds) onTarget:self withObject:nil animated:YES];
}

-(IBAction)toLocationButtonPressed
{
    toField.text = @"Current Location";
    [self toFieldChanged];
}

-(IBAction)fromLocationButtonPressed
{
    fromField.text = @"Current Location";
    [self fromFieldChanged];
}



@end
