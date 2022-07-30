package com.ancrette.gesource.db.local;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class LocalDatabaseModule {

    @Binds
    public abstract LocalDatabase bindLocalDataSource(AppDatabase appDatabase);
}
