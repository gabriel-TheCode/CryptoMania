package com.thecode.cryptomania.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thecode.cryptomania.data.local.dao.CoinDao
import com.thecode.cryptomania.data.local.dao.CoinProfileDao
import com.thecode.cryptomania.data.local.dao.ExchangeDao
import com.thecode.cryptomania.data.local.dao.GlobalMarketDao
import com.thecode.cryptomania.data.local.dao.PriceHistoryDao
import com.thecode.cryptomania.data.local.dao.WatchlistDao
import com.thecode.cryptomania.data.local.entity.CoinEntity
import com.thecode.cryptomania.data.local.entity.CoinProfileEntity
import com.thecode.cryptomania.data.local.entity.ExchangeEntity
import com.thecode.cryptomania.data.local.entity.GlobalMarketEntity
import com.thecode.cryptomania.data.local.entity.PriceHistoryEntity
import com.thecode.cryptomania.data.local.entity.WatchlistEntity

@Database(
    entities = [
        CoinEntity::class,
        GlobalMarketEntity::class,
        PriceHistoryEntity::class,
        CoinProfileEntity::class,
        ExchangeEntity::class,
        WatchlistEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class CryptoManiaDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    abstract fun globalMarketDao(): GlobalMarketDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun coinProfileDao(): CoinProfileDao
    abstract fun exchangeDao(): ExchangeDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        const val NAME = "cryptomania.db"
    }
}
