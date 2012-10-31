//
//  MapTimeViewController.h
//  MapTime
//
//  Created by Nicholas Angeli on 30/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MapTimeViewController : UIViewController

-(NSData *)downloadData;

-(void)parseXML;

@end
