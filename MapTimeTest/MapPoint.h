//
//  MapPoint.h
//  MapTimeTest
//
//  Created by Nicholas Angeli on 21/10/2012.
//  Copyright (c) 2012 MapTime. All rights reserved.
//

#import <MapKit/MapKit.h>
#import <Foundation/Foundation.h>

@interface MapPoint : NSObject <MKAnnotation> {
@public
    NSNumber *longitude;
    NSNumber *latitude;
}

-(void)insertMapPoints;

@end
