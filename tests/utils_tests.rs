#[cfg(test)]
mod tests {
    use age::{Encryptor, secrecy::SecretString};
    use books_rust::utils::encrypt;
    use books_rust::{init_logger, utils};
    use log::{debug, error, info, warn};
    use std::fs::OpenOptions;
    use std::io::Write;
    use std::{fs, path::Path};

    #[test]
    fn test_extract_pre_blocks_and_reconstruct_html() {
        let original_html = r#"
<h1>Hello</h1>
<div><pre class="rust">fn main() {
    println!("Hello, rust!");
}</pre></div>
<p>Another pre</p>
<div><pre class="rust">fn main() {
    println!("Bye!");
}</pre></div>"#;
        let utils::PreprocessedHtml(html, map) = utils::extract_pre_blocks(original_html);
        assert_eq!(2, map.len());

        init_logger();

        debug!("do do you see me?");
        info!("New html: {html}\nmap: {map:#?}");
        warn!("do do you see me?");
        error!("do do you see me?");
    }

    #[test]
    fn test_get_env_var() {
        init_logger();
        init_logger();
        info!("Hello, world 2!");
    }

    // read all the .txt files in tests/resources folder then encrypt and write to .bin files
    #[test]
    fn create_bin_files() {
        init_logger();

        let encryptor = Encryptor::with_user_passphrase(SecretString::from(books_rust::PASSWORD));

        let resources_dir = Path::new(env!("CARGO_MANIFEST_DIR")).join("tests/resources");
        let read_dir_result = fs::read_dir(resources_dir);
        if read_dir_result.is_err() {
            error!(
                "Unable to read the directory tests/resources: {:?}",
                read_dir_result.unwrap_err()
            );
            return;
        }

        for entry_result in read_dir_result.unwrap() {
            if entry_result.is_err() {
                error!(
                    "Unable to read an entry in a directory: {:?}",
                    entry_result.unwrap_err()
                );
                continue;
            }

            let dir_entry = entry_result.unwrap();
            let path_buf = dir_entry.path();
            if let Some(extension) = path_buf.extension()
                && extension == "txt"
            {
                info!("Found .txt file: {:?}", path_buf.file_name());
                let content_result = fs::read_to_string(&path_buf);
                if content_result.is_err() {
                    error!(
                        "Unable to read .txt file {:?}: {:?}",
                        &path_buf,
                        content_result.unwrap_err()
                    );
                    continue;
                }

                let content = content_result.unwrap();
                let encrypted_content_result = encrypt(content.as_str());
                if encrypted_content_result.is_err() {
                    error!(
                        "Unable to encrypt the content of the file {:?}: {:?}",
                        &path_buf,
                        encrypted_content_result.unwrap_err()
                    );
                    continue;
                }

                let encrypted_content = encrypted_content_result.unwrap();
                let mut file_result = OpenOptions::new()
                    .write(true)
                    .create(true)
                    .truncate(true)
                    .open(&path_buf);
                if file_result.is_err() {
                    error!(
                        "Unable to open/truncate file {:?} to write: {:?}",
                        &path_buf,
                        file_result.unwrap_err()
                    );
                    continue;
                }

                let mut file = file_result.unwrap();
                let write_result = file.write_all(&encrypted_content);
                if write_result.is_err() {
                    error!(
                        "Unable to write to file {:?}: {:?}",
                        &path_buf,
                        write_result.unwrap_err()
                    );
                }
            }
        }
    }
}
