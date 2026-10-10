package com.toyota.launcher.core.car

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module surface for core:car. The Singleton bindings come from the
 * @Inject constructors on the classes themselves; we keep this module as a
 * named integration point so consumers know what's offered.
 *
 * If we ever need to swap the Car connection for a fake (instrumentation
 * tests against a mock VHAL), this is where the @Binds would live.
 */
@Module
@InstallIn(SingletonComponent::class)
object CarModule
