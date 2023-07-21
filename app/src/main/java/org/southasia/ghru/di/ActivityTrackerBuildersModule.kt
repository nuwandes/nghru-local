package org.southasia.ghru.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.southasia.ghru.ui.activitytracker.activitytracker.ActivityTackeFragment
import org.southasia.ghru.ui.activitytracker.activitytracker.reason.ReasonDialogFragment
import org.southasia.ghru.ui.activitytracker.scanbarcode.ScanBarcodeFragment
import org.southasia.ghru.ui.activitytracker.scanbarcode.manualentry.ManualEntryBarcodeFragment
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.ui.stationcheck.StationCheckDialogFragment

@Suppress("unused")
@Module
abstract class ActivityTrackerBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeScanBarcodeFragment(): ScanBarcodeFragment


    @ContributesAndroidInjector
    abstract fun contributeErrorDialogFragment(): ErrorDialogFragment

    @ContributesAndroidInjector
    abstract fun contrivuteManualEntryBarcode(): ManualEntryBarcodeFragment

    @ContributesAndroidInjector
    abstract fun contrivuteActivityTackeFragment(): ActivityTackeFragment

    @ContributesAndroidInjector
    abstract fun contrivuteReasonDialogFragment(): ReasonDialogFragment

    @ContributesAndroidInjector
    abstract fun contrubuteStationCheckDialogFragment(): StationCheckDialogFragment

    @ContributesAndroidInjector
    abstract fun contrubuteCompletedDialogFragment(): org.southasia.ghru.ui.activitytracker.activitytracker.completed.CompletedDialogFragment
}
