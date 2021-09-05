package com.example.fountainfinder.db.local;

import android.content.Context;
import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AppDatabaseModule {

    @Provides
    public FountainDao provideFountainDao(AppDatabase appDatabase) {
        return appDatabase.fountainDao();
    }

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "fl_database")
                // Wipes and rebuilds instead of migrating
                // if no Migration object.
                .fallbackToDestructiveMigration()
                .build();
    }
}

