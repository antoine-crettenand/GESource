package com.example.fountainfinder.db;

import com.example.fountainfinder.db.local.LocalDataSource;
import com.example.fountainfinder.db.remote.scrapper.GESoifScrapper;
import com.example.fountainfinder.db.remote.scrapper.RemoteDataSource;
import com.example.fountainfinder.db.sanitizer.DataSanitizer;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class FountainDataRepositoryModule {

    @Provides
    @Singleton
    public RemoteDataSource providesRemoteDataSource() {
        return new GESoifScrapper();
    }

    @Provides
    @Singleton
    public FountainDataRepository providesFountainDataRepository(LocalDataSource localDataSource, RemoteDataSource remoteDataSource, DataSanitizer dataSanitizer) {
        return new FountainDataRepository(localDataSource, remoteDataSource, dataSanitizer);
    }
}
