
#import <MapKit/MapKit.h>
#import <Foundation/Foundation.h>

@interface MapPoint : NSObject <MKAnnotation> {
@public
    NSNumber *longitude;
    NSNumber *latitude;
    NSString *title;
}

-(NSString *)title;

@property (nonatomic, strong) NSString *title;

@end