set FromDate to date "%s"
set ToDate to date "%s"
set EventId to 0
tell application "Microsoft Outlook"
	set CalEvents to {}
	repeat with i from 1 to number of items in calendars
		if name of item i of calendars is "%s" then
			repeat with j from 1 to number of items of calendar events of item i of calendars
				set CalEvent to item j of calendar events of item i of calendars
				if start time of CalEvent is greater than FromDate and end time of CalEvent is less than ToDate and is recurring of CalEvent is false and all day flag of CalEvent is false then
					set EventId to EventId + 1
					set eventContent to (content of CalEvent as string)
					if eventContent = missing value then set eventContent to ""
					set eventSubject to (subject of CalEvent)
					if eventSubject = missing value then set eventSubject to ""
					set eventStart to (start time of CalEvent)
					if eventStart = missing value then set eventStart to ""
					set eventEnd to (end time of CalEvent)
					if eventEnd = missing value then set eventEnd to ""
					set eventCat to category of CalEvent
					set eventCategories to ""
					if not category of CalEvent = {} then
						repeat with k from 1 to number of items of eventCat
							set eventCategories to eventCategories & name of item k of eventCat
						end repeat
					end if
					set CalEvents to CalEvents & EventId & "Subject" & eventSubject
					set CalEvents to CalEvents & EventId & "Start" & eventStart
					set CalEvents to CalEvents & EventId & "End" & eventEnd
					set CalEvents to CalEvents & EventId & "Categories" & eventCategories
					set CalEvents to CalEvents & EventId & "Body" & eventContent
				end if
				if is recurring of CalEvent is true and all day flag of CalEvent is false then
				    set offsetFromStart to start time of CalEvent
				    set offsetFromStart to time of offsetFromStart
					set reqPattern to recurrence of CalEvent
					set nextDateStart to (start date of reqPattern) - (1 * days)
					set stopDate to ToDate
					if (end type of end date of reqPattern) = end date type and (data of end date of reqPattern is less than ToDate) then set stopDate to (data of end date of reqPattern)
					set MaxReqEvents to 0
					set ReqEventCounter to 0
					if (end type of end date of reqPattern) = end numbered type then set MaxReqEvents to data of end date of reqPattern
					if nextDateStart is less than FromDate then set nextDateStart to FromDate - (1 * days)
					repeat (stopDate - FromDate) div days times
						if recurrence type of reqPattern = daily then
							repeat until nextDateStart is greater than stopDate
								set nextDateStart to nextDateStart + (1 * days)
								set dayFromStart to (nextDateStart - (start date of reqPattern)) div (1 * days)
								if dayFromStart mod (occurrence interval of reqPattern) = 0 then exit repeat
							end repeat
						end if
						if recurrence type of reqPattern = weekly then
							repeat until nextDateStart is greater than stopDate
								set nextDateStart to nextDateStart + (1 * days)
								set weekFromStart to (nextDateStart - ((start date of reqPattern) - (((start date of reqPattern)'s weekday as number) * days))) div (7 * days)
								if not weekFromStart mod (occurrence interval of reqPattern) = 0 then
									repeat until nextDateStart is greater than stopDate
										set nextDateStart to nextDateStart + (1 * days)
										set weekFromStart to (nextDateStart - ((start date of reqPattern) - (((start date of reqPattern)'s weekday as number) * days))) div (7 * days)
										if weekFromStart mod (occurrence interval of reqPattern) = 0 then exit repeat
									end repeat
								end if
								if monday of days of week of reqPattern and nextDateStart's weekday as number = 2 then exit repeat
								if tuesday of days of week of reqPattern and nextDateStart's weekday as number = 3 then exit repeat
								if wednesday of days of week of reqPattern and nextDateStart's weekday as number = 4 then exit repeat
								if thursday of days of week of reqPattern and nextDateStart's weekday as number = 5 then exit repeat
								if friday of days of week of reqPattern and nextDateStart's weekday as number = 6 then exit repeat
								if saturday of days of week of reqPattern and nextDateStart's weekday as number = 7 then exit repeat
								if sunday of days of week of reqPattern and nextDateStart's weekday as number = 1 then exit repeat
							end repeat
						end if
						if recurrence type of reqPattern = absolute monthly or recurrence type of reqPattern = relative monthly or recurrence type of reqPattern = absolute yearly or recurrence type of reqPattern = relative yearly then
							-- if all days of days of week of reqPattern then set reqWD to reqWD & monday & tuesday & wednsday & thursday & friday & saturday & sunday
							-- if weekdays of days of week of reqPattern then set reqWD to reqWD & monday & tuesday & wednsday & thursday & friday
							-- if weekends of days of week of reqPattern then set reqWD to reqWD & saturday & sunday
						end if
						if nextDateStart is greater than stopDate then exit repeat
						set ReqEventCounter to ReqEventCounter + 1
						if not MaxReqEvents = 0 and ReqEventCounter is greater than MaxReqEvents then exit repeat
						set EventId to EventId + 1
						set eventContent to (content of CalEvent as string)
						if eventContent = missing value then set eventContent to ""
						set eventSubject to (subject of CalEvent)
						if eventSubject = missing value then set eventSubject to ""
						set eventStart to (nextDateStart + offsetFromStart)
						if eventStart = missing value then set eventStart to ""
						set eventEnd to (nextDateStart + offsetFromStart + ((end time of CalEvent) - (start time of CalEvent)))
						if eventEnd = missing value then set eventEnd to ""
						set eventCat to category of CalEvent
						set eventCategories to ""
						if not category of CalEvent = {} then
							repeat with k from 1 to number of items of eventCat
								set eventCategories to eventCategories & name of item k of eventCat
							end repeat
						end if
						set CalEvents to CalEvents & EventId & "Subject" & eventSubject
						set CalEvents to CalEvents & EventId & "Start" & eventStart
						set CalEvents to CalEvents & EventId & "End" & eventEnd
						set CalEvents to CalEvents & EventId & "Categories" & eventCategories
						set CalEvents to CalEvents & EventId & "Body" & eventContent
					end repeat
				end if
			end repeat
		end if
	end repeat
	return CalEvents
end tell