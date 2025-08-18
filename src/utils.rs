use crate::PASSWORD;
use age::{Encryptor, secrecy::SecretString};
use regex::{Captures, Regex};
use std::borrow::Cow;
use std::collections::HashMap;
use std::error::Error;
use std::io::Write;

#[derive(Debug)]
pub struct PreprocessedHtml(pub String, pub HashMap<usize, String>);

pub fn extract_pre_blocks(html: &str) -> PreprocessedHtml {
    let regex = Regex::new(r"(?s)<pre.*?</pre>").unwrap();
    let mut result_html = String::new();
    let mut pre_map = HashMap::<usize, String>::new();
    let mut id: usize = 0;
    let mut last_end = 0; // the position after the last </pre>

    for matc in regex.find_iter(html) {
        result_html.push_str(&html[last_end..matc.start()]);
        result_html.push_str(&format!(r#"<i id="{id}"></i>"#));
        pre_map.insert(id, matc.as_str().to_string());
        last_end = matc.end();
        id += 1;
    }

    result_html.push_str(&html[last_end..]);
    PreprocessedHtml(result_html, pre_map)
}

pub fn reconstruct_html(pre_map: &HashMap<usize, String>, processed_html: &str) -> String {
    let regex = Regex::new(r#"<i id="(\d+)"></i>"#).unwrap();
    let reconstructed: Cow<str> = regex.replace_all(processed_html, |captures: &Captures| {
        let id = captures[1]
            .parse::<usize>()
            .expect("Unable to parse to usize");
        pre_map.get(&id).cloned().unwrap_or_default()
    });
    reconstructed.into_owned()
}

pub fn encrypt(text: &str) -> Result<Vec<u8>, Box<dyn Error>> {
    let encryptor = Encryptor::with_user_passphrase(SecretString::from(PASSWORD));
    let mut encrypted_data: Vec<u8> = vec![];
    let mut writer = encryptor.wrap_output(&mut encrypted_data).unwrap();
    writer.write_all(text.as_bytes())?;
    writer.finish()?;
    Ok(encrypted_data)
}

#[derive(serde::Serialize, serde::Deserialize, Debug)]
pub struct Dog {
    pub name: String,
    pub age: u8,
}

#[derive(serde::Serialize, serde::Deserialize, Debug)]
pub struct Fish {
    pub name: String,
    pub heavy: Option<bool>,
    pub dogs: Vec<Dog>,
}
