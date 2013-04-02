package me.sonar.sdkmanager.core

import org.joda.time.DateTime
import java.util.Date
import org.springframework.core.convert.converter.Converter

class DateTimeToDateConverter extends Converter[DateTime, Date] {
    def convert(source: DateTime) = source.toDate
}

class DateToDateTimeConverter extends Converter[Date, DateTime] {
    def convert(source: Date) = new DateTime(source)
}
