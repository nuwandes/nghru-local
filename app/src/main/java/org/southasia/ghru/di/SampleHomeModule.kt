package org.southasia.ghru.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.southasia.ghru.ui.samplemanagement.SampleMangementFragment

@Suppress("unused")
@Module
abstract class SampleHomeModule {
    @ContributesAndroidInjector
    abstract fun contributeSampleMangementFragment(): SampleMangementFragment

}
