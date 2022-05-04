package com.ancrette.gesource.db;

import android.content.Context;
import com.ancrette.gesource.db.local.LocalDataSource;
import com.ancrette.gesource.db.remote.RemoteDataSource;
import com.ancrette.gesource.db.remote.aws.AWSRemoteInterface;
import com.ancrette.gesource.db.remote.scrapper.GESoifScrapper;
import com.ancrette.gesource.db.sanitizer.DataSanitizer;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class FountainDataRepositoryModule {

    @Provides
    @Singleton
    public RemoteDataSource providesRemoteDataSource(@ApplicationContext Context context) {
        return new AWSRemoteInterface(new GESoifScrapper());
    }

    @Provides
    @Singleton
    public FountainDataRepository providesFountainDataRepository(LocalDataSource localDataSource, RemoteDataSource remoteDataSource, DataSanitizer dataSanitizer) {
        return new FountainDataRepository(localDataSource, remoteDataSource, dataSanitizer);
    }
}
