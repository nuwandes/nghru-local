package org.southasia.ghru.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.southasia.ghru.ui.home.HomeFragment

@Suppress("unused")
@Module
abstract class ScreeningHomeModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

}
