package com.ancrette.gesource.db.sanitizer;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class SanitizerModule {

    @Provides
    @Singleton
    public DataSanitizer providesSanitizer(){
        return new DataSanitizer();
    }
}
