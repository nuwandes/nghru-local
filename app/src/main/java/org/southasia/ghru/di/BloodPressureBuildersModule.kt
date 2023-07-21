package org.southasia.ghru.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.southasia.ghru.ui.bodymeasurements.bp.BPFragment
import org.southasia.ghru.ui.bodymeasurements.bp.info.InfoFragment
import org.southasia.ghru.ui.bodymeasurements.bp.main.BPMainFragment
import org.southasia.ghru.ui.bodymeasurements.bp.manual.one.BPManualOneFragment
import org.southasia.ghru.ui.bodymeasurements.bp.manual.three.BPManualThreeFragment
import org.southasia.ghru.ui.bodymeasurements.bp.manual.two.BPManualTwoFragment
import org.southasia.ghru.ui.bodymeasurements.bp.skip.SkipDialogFragment
import org.southasia.ghru.ui.bodymeasurements.manualentrybp.ManualEntryBodyMeasurement
import org.southasia.ghru.ui.bodymeasurements.measurements.MeasurementsFragment
import org.southasia.ghru.ui.bodymeasurements.measurements.second.MeasurementsSecondFragment
import org.southasia.ghru.ui.bodymeasurements.review.ReviewFragment
import org.southasia.ghru.ui.bodymeasurements.review.completed.CompletedDialogFragment
import org.southasia.ghru.ui.bodymeasurements.scanbarcodebp.ScanBarcodeFragment
import org.southasia.ghru.ui.bodymeasurements.verifyid.VerifyIDFragment
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.ui.stationcheck.StationCheckDialogFragment

@Suppress("unused")
@Module
abstract class BloodPressureBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeBPFragment(): BPFragment

    @ContributesAndroidInjector
    abstract fun contributeMeasurementsFragment(): MeasurementsFragment

    @ContributesAndroidInjector
    abstract fun contributeReviewFragment(): ReviewFragment

    @ContributesAndroidInjector
    abstract fun contributeVerifyIDFragment(): VerifyIDFragment

    @ContributesAndroidInjector
    abstract fun contributeScanBarcodeFragment(): ScanBarcodeFragment


    @ContributesAndroidInjector
    abstract fun contributeMeasurementsSecondFragment(): MeasurementsSecondFragment

    @ContributesAndroidInjector
    abstract fun contributeBPMainFragment(): BPMainFragment

    @ContributesAndroidInjector
    abstract fun contributeBPManualOneFragment(): BPManualOneFragment

    @ContributesAndroidInjector
    abstract fun contributeBPManualTwoFragment(): BPManualTwoFragment

    @ContributesAndroidInjector
    abstract fun contributeBPManualThreeFragment(): BPManualThreeFragment

    @ContributesAndroidInjector
    abstract fun contributeSkipDialogFragment(): SkipDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeCompletedDialogFragment(): CompletedDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeInfoFragment(): InfoFragment

    @ContributesAndroidInjector
    abstract fun contributeErrorDialogFragment(): ErrorDialogFragment

    @ContributesAndroidInjector
    abstract fun contrubuteManualEntryBodyMeasurement(): ManualEntryBodyMeasurement

    @ContributesAndroidInjector
    abstract fun contrubuteStationCheckDialogFragment(): StationCheckDialogFragment

    @ContributesAndroidInjector
    abstract fun contrubuteReasonDialogFragment(): org.southasia.ghru.ui.bodymeasurements.bp.reason.ReasonDialogFragment

    @ContributesAndroidInjector
    abstract fun contrubuteCompletedDialogFragment(): org.southasia.ghru.ui.bodymeasurements.bp.completed.CompletedDialogFragment

}