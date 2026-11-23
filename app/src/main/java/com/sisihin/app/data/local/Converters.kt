package com.sisihin.app.data.local

import androidx.room.TypeConverter
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.GoalStatus
import com.sisihin.app.domain.model.PlanFrequency
import com.sisihin.app.domain.model.TransactionType

class Converters {
    @TypeConverter fun planFreqToString(v: PlanFrequency): String = v.name
    @TypeConverter fun stringToPlanFreq(v: String): PlanFrequency = PlanFrequency.valueOf(v)

    @TypeConverter fun statusToString(v: GoalStatus): String = v.name
    @TypeConverter fun stringToStatus(v: String): GoalStatus = GoalStatus.valueOf(v)

    @TypeConverter fun txTypeToString(v: TransactionType): String = v.name
    @TypeConverter fun stringToTxType(v: String): TransactionType = TransactionType.valueOf(v)

    @TypeConverter fun currencyToString(v: CurrencyCode): String = v.name
    @TypeConverter fun stringToCurrency(v: String): CurrencyCode = CurrencyCode.valueOf(v)
}