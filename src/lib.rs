pub mod utils;

use colored::Colorize;
use env_logger::{Builder, Env, Target};
use log::Level;
use std::io::Write;
use time::OffsetDateTime;

pub const PASSWORD: &str = "...";

/*
 * If MY_LOG_LEVEL is info,utils_tests=debug, then the default logging level threshold is info, but
 * for the module utils_tests, it is debug.
 */
pub fn init_logger() {
    let env = Env::default().filter_or("MY_LOG_LEVEL", "info");

    let _ = Builder::from_env(env)
        .format(|buf, record| {
            // Get current timestamp
            let timestamp = OffsetDateTime::now_local().unwrap();
            let format =
                time::format_description::parse("[year]-[month]-[day] [hour]:[minute]:[second].[subsecond digits:3]")
                    .unwrap();

            // Apply colors to different parts of the log message using the colored crate
            let level = match record.level() {
                Level::Error => record.level().to_string().red().bold(),
                Level::Warn => record.level().to_string().yellow().bold(),
                Level::Info => record.level().to_string().green(),
                Level::Debug => record.level().to_string().cyan(),
                Level::Trace => record.level().to_string().magenta(),
            };

            let module = record.module_path().unwrap_or("unknown").blue();
            let message = record.args().to_string().white();

            // Format the log message: timestamp [module] [level] message
            writeln!(buf, "{} [{}] [{}] {}", timestamp.format(&format).unwrap(), module, level, message)
        })
        .target(Target::Stdout) // it logs to stderr by default
        .try_init();
}
