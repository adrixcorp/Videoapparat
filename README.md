# Videoapparat

![Build status](https://travis-ci.org/Fotoapparat/Videoapparat.svg?branch=master)


![ ](sample/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

The best and only Android library to take pictures and videos. Period.

Fotoapparat + Video = Videoapparat

Videoapparat is a premium library which allows you to also record videos.


## How it works
### Usage

It has exactly the same api and classes as Fotoapparat, so you don't need to migrate anything.
 
Simply, two additional methods `startRecording/stopRecording` in the existing `Fotoapparat` object to record a video. 

```kotlin
val fotoapparat = Fotoapparat(
    context = this,
    view = cameraView
)
 
fotoapparat.start()
 
// take a picture
fotoapparat.takePicture()
 
// or record a video    
fotoapparat.startRecording(someFile)
fotoapparat.stopRecording()
```

To see Fotoapparat usage [visit here](https://github.com/Fotoapparat/Fotoapparat#how-it-works).


### Install 
Add dependency to your `build.gradle`

```groovy
repositories {
  maven { url 'http://artifactory.fotoapparat.io:8081/artifactory/products' }
}
 
implementation 'io.videoapparat:library:1.0.4'
```
 
Camera and record audio permission will be automatically added to your `AndroidManifest.xml`. Do not forget to request this permission on Marshmallow and higher.

### Migrate from Fotoapparat
Change the dependencies in your `build.gradle`

```groovy
repositories {
  // optionally remove jitpack repo
  maven { url 'https://jitpack.io' }
  
  // add videoapparat artifactory:
  maven { url 'http://artifactory.fotoapparat.io:8081/artifactory/products' }
}
 
// remove fotoapparat dependency
implementation 'io.fotoapparat.fotoapparat:library:2.2.0'
 
// and replace it with videoapparat
implementation 'io.videoapparat:library:1.0.4'
```

### Full version

This is a demo/sample app which allows 3 seconds long videos.
To allow recording unlimited length videos, please [contact our sales team](http://fotoapparat.io/).

To add your license call `setLicence` *before* any other interaction with the library. Ideally in `Application#onCreate`.

```kotlin
Fotoapparat.setLicence("theLicenseWeWillProvideYou")
 
Fotoapparat(
    context = this,
    view = cameraView
)
```

## License

```
Copyright 2018 Fotoapparat

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
