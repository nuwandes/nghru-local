package org.southasia.ghru.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.southasia.ghru.ui.fundoscopy.displaybarcode.DisplayBarcodeFragment
import org.southasia.ghru.ui.fundoscopy.guide.ElectrodeFragment
import org.southasia.ghru.ui.fundoscopy.guide.GuideFragment
import org.southasia.ghru.ui.fundoscopy.guide.PreperationFragment
import org.southasia.ghru.ui.fundoscopy.guide.main.GuideMainFragment
import org.southasia.ghru.ui.fundoscopy.manualentry.ManualEntryFundoscopyFragment
import org.southasia.ghru.ui.fundoscopy.reading.FundoscopyReadingFragment
import org.southasia.ghru.ui.fundoscopy.reading.reason.ReasonDialogFragment
import org.southasia.ghru.ui.fundoscopy.scanbarcode.ScanBarcodeFragment
import org.southasia.ghru.ui.fundoscopy.verifyid.FundoscopyVerifyIDFragment
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.ui.stationcheck.StationCheckDialogFragment


@Suppress("unused")
@Module
abstract class FundoscopyBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFundoscopyVerifyIDFragment(): FundoscopyVerifyIDFragment

    @ContributesAndroidInjector
    abstract fun contributeScanBarcodeFragment(): ScanBarcodeFragment


    @ContributesAndroidInjector
    abstract fun contributeDisplayBarcodeFragment(): DisplayBarcodeFragment

    @ContributesAndroidInjector
    abstract fun contributeGuideMainFragment(): GuideMainFragment

    @ContributesAndroidInjector
    abstract fun contributeGuideFragment(): GuideFragment

    @ContributesAndroidInjector
    abstract fun contributeElectrodeFragment(): ElectrodeFragment

    @ContributesAndroidInjector
    abstract fun contributePreperationFragment(): PreperationFragment

    @ContributesAndroidInjector
    abstract fun contributeFundoscopyReadingFragment(): FundoscopyReadingFragment

    @ContributesAndroidInjector
    abstract fun contributeReasonDialogFragment(): ReasonDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeErrorDialogFragment(): ErrorDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeManualEntryFundoscopyFragment(): ManualEntryFundoscopyFragment

    @ContributesAndroidInjector
    abstract fun contrubuteStationCheckDialogFragment(): StationCheckDialogFragment

    @ContributesAndroidInjector
    abstract fun contrubuteCompletedDialogFragment(): org.southasia.ghru.ui.fundoscopy.reading.completed.CompletedDialogFragment
}

