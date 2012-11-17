//
//  MainViewController.h
//  MapTime
//
//  Created by Nicholas Angeli on 07/11/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TimeLineDownloaderDelegate.h"

@interface MainViewController : UIViewController <UIPickerViewDelegate>
{
    UIPickerView *picker;
    NSMutableArray *contents;
    TimeLineDownloaderDelegate *delegate;
}
@property (strong, nonatomic) IBOutlet UITextField *fromField;
@property (strong, nonatomic) IBOutlet UITextField *toField;
 
-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender;

-(BOOL)textFieldShouldReturn:(UITextField *)textField;

-(void)traverseTimeLineElement:(TBXMLElement *)element withTimeLineObject:(TimeLine *)timeLine;

-(void)traverseElement:(TBXMLElement *)element;

-(void)parseXML:(NSData *)xml;

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component;

-(NSInteger)pickerView:(UIPickerView *)thePickerView numberOfRowsInComponent:(NSInteger)component;

-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)thePickerView;

-(void)downloadTimelines;

-(void)viewDidLoad;

-(void)useNotification;

@end
