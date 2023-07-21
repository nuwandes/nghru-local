package org.southasia.ghru.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.southasia.ghru.ui.datamanagement.DataManagementListFragment
import org.southasia.ghru.ui.devices.DevicesFragment
import org.southasia.ghru.ui.enumeration.EnumerationFragment
import org.southasia.ghru.ui.home.HomeFragment
import org.southasia.ghru.ui.homeenumeration.HomeEnumerationFragment
import org.southasia.ghru.ui.homeenumerationlist.HomeEmumerationListFragment
import org.southasia.ghru.ui.logout.LogoutDialogFragment
import org.southasia.ghru.ui.samplemanagement.SampleMangementFragment
import org.southasia.ghru.ui.station.StationFragment
import org.southasia.ghru.ui.usersetting.UserSettingFragment

@Suppress("unused")
@Module
abstract class MainFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeEnumerationFragment(): EnumerationFragment

    @ContributesAndroidInjector
    abstract fun contributeHomeEmumerationListFragment(): HomeEmumerationListFragment

    @ContributesAndroidInjector
    abstract fun contributeHomeEnumerationFragment(): HomeEnumerationFragment

    @ContributesAndroidInjector
    abstract fun contributeStationFragment(): StationFragment

    @ContributesAndroidInjector
    abstract fun contributeDevicesFragment(): DevicesFragment


    @ContributesAndroidInjector
    abstract fun contributeSampleMangementFragment(): SampleMangementFragment

    @ContributesAndroidInjector
    abstract fun contributeUserSettingFragment(): UserSettingFragment

    @ContributesAndroidInjector
    abstract fun contributeLogoutDialogFragment(): LogoutDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeDataManagementListFragment() : DataManagementListFragment

}
