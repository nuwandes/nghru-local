package org.southasia.ghru.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.southasia.ghru.ui.setting.SettingFragment

@Suppress("unused")
@Module
abstract class SettingFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingFragment(): SettingFragment


}
