package com.wickedcoder.app.core.di

import android.content.Context
import androidx.room.Room
import com.wickedcoder.app.data.local.AppDatabase
import com.wickedcoder.app.data.local.dao.TransactionDao
import com.wickedcoder.app.data.repository.TransactionRepositoryImpl
import com.wickedcoder.app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    companion object {

        @Provides @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "spendsense.db")
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .fallbackToDestructiveMigration(false)
                .build()

        @Provides @Singleton
        fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    }
}
