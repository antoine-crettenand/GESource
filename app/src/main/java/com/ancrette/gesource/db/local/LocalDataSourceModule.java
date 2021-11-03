package com.ancrette.gesource.db.local;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class LocalDataSourceModule {

    @Binds
    public abstract LocalDataSource bindLocalDataSource(AppDatabase appDatabase);
}
