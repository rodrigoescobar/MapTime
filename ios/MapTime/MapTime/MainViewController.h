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
    NSMutableData *timeLineData;
    NSMutableArray *timeLines;
    TimeLineDownloaderDelegate *delegate;
}
@property (strong, nonatomic) IBOutlet UITextField *fromField;
@property (strong, nonatomic) IBOutlet UITextField *toField;
@property (strong, nonatomic) IBOutlet UIButton *testButton;

-(IBAction)testButtonClicked:(id)sender;

@end
