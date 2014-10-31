/*
	This code is based upon StringScanner in the standard Ruby library
*/
StringScanner {
	var string, <pos,
		defaultPeekLength = 5,
		debugStringScanner = nil // nil = no debug, notNil = debug
	;

	*new { |string| ^super.new.initStringScanner(string) }

	initStringScanner { |argString|
		pos = 0;
		string = argString;
	}

	pos_ { |argPos|
		if (argPos.inclusivelyBetween(0, this.prEosPos)) {
			pos = argPos
		} {
			Error("pos must be between 0 and %".format(this.prEosPos)).throw
		}
	}

	matches { |regexp|
		var match;
		match = this.prFindRegexpDirectlyAfterPos(regexp);
		match.notNil.if {
			debugStringScanner !? { "matched: '%'".format(match).debug };
			^match.size
		} { ^nil }
	}

	scan { |regexp|
		var match;
		match = this.prFindRegexpDirectlyAfterPos(regexp);
		match.notNil.if {
			debugStringScanner !? { "scanned: '%'".format(match).debug };
			pos = pos + match.size;
			^match
		} { ^nil }
	}

	scanUntil { |regexp|
		var matchData;
		matchData = this.prFindFirstRegexpAfterPos(regexp);
		matchData.notNil.if {
			pos = pos + matchData[0] + matchData[1].size;
			^matchData[1]
		} { ^nil }
	}

	skip { |regexp|
		var match;
		match = this.prFindRegexpDirectlyAfterPos(regexp);
		match.notNil.if {
			debugStringScanner !? { "skipped: '%'".format(match).debug };
			pos = pos + match.size;
			^match.size
		} { ^nil }
	}

	skipUntil { |regexp|
		var matchData;
		matchData = this.prFindFirstRegexpAfterPos(regexp);
		matchData.notNil.if {
			pos = pos + matchData[0] + matchData[1].size;
			^matchData[1].size
		} { ^nil }
	}

	getChar {
		var char;
		char = string[pos];
		pos = pos + 1;
		^char
	}

	reset { pos = 0 }

	eos { ^this.atEndOfString }
	bos { ^this.atBeginningOfString }

	atEndOfString { ^pos == this.prEosPos }
	atBeginningOfString { ^pos == 0 }

	peek { |argLength=nil|
		var length = argLength ? defaultPeekLength;
		^string[pos..(pos+length-1)]
	}

	asString {
		^super.asString +
			this.atEndOfString.if {
				"fin"
			} {
				"%/%".format(pos, this.prEosPos) +
				this.atBeginningOfString.if {
					"@" + this.prAfterPos.quote
				} {
					this.prBeforePos.quote + "@" + this.prAfterPos.quote
				}
			}
	}

	// private
	prFindRegexpDirectlyAfterPos { |regexp|
		var matchData;
		matchData = this.prFindFirstRegexp(string, regexp, pos);
		^matchData.notNil.if {
			if (matchData[0] == pos) {
				matchData[1]
			} { nil }
		} { nil }
	}

	prFindFirstRegexpAfterPos { |regexp|
		var matchData;
		matchData = this.prFindFirstRegexp(string, regexp, pos);
		^matchData.notNil.if { [matchData[0]-pos, matchData[1]] } { nil }
	}

	prFindFirstRegexp { |string, regexp, offset|
		^string.findRegexp(regexp, pos).first
	}

	prBeforePos {
		var start = max(0, pos-defaultPeekLength),
			end = pos-1;
		^if (start <= 0) {""} {"..."} ++ string[start..end].asString
	}

	prAfterPos {
		var start = pos,
			end = min(string.size-1, pos+defaultPeekLength-1);
		^string[start..end].asString ++ if (end == (string.size-1)) {""} {"..."}
	}

	prEosPos { ^string.size }
}
