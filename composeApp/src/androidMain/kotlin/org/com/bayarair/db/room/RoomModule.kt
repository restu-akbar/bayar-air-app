package org.com.bayarair.db.room
// 
// import androidx.room.Room
// import org.koin.android.ext.koin.androidContext
// import org.koin.dsl.module
// 
// val roomModule =
//     module {
//         single {
//             Room
//                 .databaseBuilder(
//                     androidContext(),
//                     AppDatabase::class.java,
//                     "app.db",
//                 ).fallbackToDestructiveMigration() // ganti sesuai strategi
//                 .build()
//         }
//         single<SomeDao> { get<AppDatabase>().someDao() }
//     }
