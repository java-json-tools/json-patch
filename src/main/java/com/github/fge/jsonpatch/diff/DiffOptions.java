package com.github.fge.jsonpatch.diff;

/**
 * Created by AMA on 24/09/2020.
 */
public class DiffOptions {

		public static final boolean DIFF_DOESNT_REQUIRE_SOURCE = false;

		final boolean diffDoesntRequireSource;

		public static final DiffOptions DEFAULT_OPTIONS = new DiffOptions(DIFF_DOESNT_REQUIRE_SOURCE);

		private DiffOptions(boolean diffDoesntRequireSource) {
				this.diffDoesntRequireSource = diffDoesntRequireSource;
		}

		public boolean isDiffDoesntRequireSource() {
				return diffDoesntRequireSource;
		}

		public static class Builder {
				private boolean diffDoesntRequireSource = DIFF_DOESNT_REQUIRE_SOURCE;

				public Builder diffDoesntRequireSource() {
						diffDoesntRequireSource = true;
						return this;
				}

				public Builder diffRequireSource() {
						diffDoesntRequireSource = false;
						return this;
				}

				public DiffOptions build(){
						return new DiffOptions(diffDoesntRequireSource);
				}

		}
}
