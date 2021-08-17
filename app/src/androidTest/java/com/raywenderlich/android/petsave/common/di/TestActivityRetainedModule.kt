package com.raywenderlich.android.petsave.common.di

import com.raywenderlich.android.petsave.common.data.FakeRepository
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import com.raywenderlich.android.petsave.common.utils.CoroutineDispatchersProvider
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.disposables.CompositeDisposable

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class TestActivityRetainedModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindsAnimalsRepository(animalsRepository: FakeRepository): AnimalRepository

    @Binds
    abstract fun bindsDispatchersProvider(provider: CoroutineDispatchersProvider): DispatchersProvider

    companion object {
        @Provides
        fun providesCompositeDisposable() =
            CompositeDisposable()
    }
}